import os
import time
from io import BytesIO
from pathlib import Path
from typing import Optional

from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from PIL import Image
from ultralytics import YOLO

BASE_DIR = Path(__file__).resolve().parent.parent
MODEL_PATH = Path(os.getenv("SEAFISH_MODEL_PATH", BASE_DIR / "models" / "best.pt"))
MAX_FILE_SIZE = 10 * 1024 * 1024
ALLOWED_CONTENT_TYPES = {"image/jpeg", "image/png", "image/webp"}
CLASS_DISPLAY_NAMES = {
    "Crabs": "螃蟹",
    "Dolphin": "海豚",
    "JellyFish": "水母",
    "Lobster": "龙虾",
    "Otter": "海獭",
    "Penguin": "企鹅",
    "Seal": "海豹",
    "Sharks": "鲨鱼",
    "Starfish": "海星",
    "Urchins": "海胆",
}

app = FastAPI(
    title="SeaFish YOLO Service",
    description="海洋鱼类智能化信息管理系统的独立目标检测服务",
    version="1.0.0",
)

model: Optional[YOLO] = None


def get_model() -> YOLO:
    global model
    if model is None:
        if not MODEL_PATH.exists():
            raise RuntimeError(f"模型文件不存在：{MODEL_PATH}")
        model = YOLO(str(MODEL_PATH))
    return model


@app.get("/health")
def health() -> dict:
    return {
        "status": "UP",
        "service": "seafish-yolo",
        "modelExists": MODEL_PATH.exists(),
        "modelPath": MODEL_PATH.name,
    }


@app.post("/api/v1/detect")
async def detect(
    image: UploadFile = File(...),
    confidence_threshold: float = Form(0.5),
) -> dict:
    if image.content_type not in ALLOWED_CONTENT_TYPES:
        raise HTTPException(400, "仅支持 JPG、PNG 或 WEBP 图片")
    if not 0.1 <= confidence_threshold <= 1.0:
        raise HTTPException(400, "置信度阈值必须在 0.1 到 1 之间")

    content = await image.read()
    if not content or len(content) > MAX_FILE_SIZE:
        raise HTTPException(400, "图片为空或超过 10MB")

    try:
        source = Image.open(BytesIO(content)).convert("RGB")
    except Exception as exc:
        raise HTTPException(400, "图片文件已损坏或无法解析") from exc

    started = time.perf_counter()
    try:
        result = get_model().predict(
            source=source,
            conf=confidence_threshold,
            device="cpu",
            verbose=False,
        )[0]
    except Exception as exc:
        raise HTTPException(503, f"模型推理失败：{exc}") from exc

    names = result.names
    targets = []
    if result.boxes is not None:
        for box in result.boxes:
            x1, y1, x2, y2 = box.xyxy[0].tolist()
            class_id = int(box.cls[0].item())
            raw_class_name = str(names[class_id])
            targets.append(
                {
                    "className": CLASS_DISPLAY_NAMES.get(raw_class_name, raw_class_name),
                    "confidence": round(float(box.conf[0].item()), 6),
                    "boxX1": round(x1, 4),
                    "boxY1": round(y1, 4),
                    "boxX2": round(x2, 4),
                    "boxY2": round(y2, 4),
                }
            )

    rendered = result.plot()
    rendered_rgb = rendered[:, :, ::-1]
    output = BytesIO()
    Image.fromarray(rendered_rgb).save(output, format="JPEG", quality=90)

    return {
        "modelName": MODEL_PATH.stem,
        "durationMs": round((time.perf_counter() - started) * 1000),
        "targets": targets,
        "resultImageBase64": __import__("base64").b64encode(output.getvalue()).decode("ascii"),
    }


@app.get("/api/v1/model")
def model_info() -> dict:
    loaded_model = get_model()
    return {"modelName": MODEL_PATH.stem, "classes": loaded_model.names}
