# SeaFish AI 识别服务

这是项目的独立 Python YOLO 服务。Spring Boot 负责用户、权限和业务数据，FastAPI 只负责模型推理。

## 启动

```powershell
cd ai-service
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
.\.venv\Scripts\python.exe -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

打开 `http://localhost:8000/docs` 可以查看 FastAPI 自动生成的接口文档。

模型来自原毕业设计的 `best.pt`，包含 Crabs、Dolphin、JellyFish、Lobster、Otter、Penguin、Seal、Sharks、Starfish、Urchins 十类海洋生物。
