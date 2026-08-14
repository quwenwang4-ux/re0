# SeaFish AI 识别服务

这是项目的独立 Python YOLO 服务。Spring Boot 负责用户、权限和业务数据，FastAPI 只负责模型推理。

## 启动

请使用 64 位 Python 3.11。Windows 32 位 Python 无法安装 PyTorch，因此不能运行 YOLO。

```powershell
cd ai-service
py -3.11 -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
.\.venv\Scripts\python.exe -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

如果 pip 报代理或 `check_hostname requires server_hostname` 错误，请在当前 PowerShell 中先执行：

```powershell
$env:NO_PROXY='*'
$env:no_proxy='*'
Remove-Item Env:HTTP_PROXY -ErrorAction Ignore
Remove-Item Env:HTTPS_PROXY -ErrorAction Ignore
Remove-Item Env:ALL_PROXY -ErrorAction Ignore
```

打开 `http://localhost:8000/docs` 可以查看 FastAPI 自动生成的接口文档。

模型来自原毕业设计的 `best.pt`，包含 Crabs、Dolphin、JellyFish、Lobster、Otter、Penguin、Seal、Sharks、Starfish、Urchins 十类海洋生物。
