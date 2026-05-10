# Auction Microservice Milestone 2 Postman Package

Import these two files into Postman:

1. `AuctionMicroservice_MS2.postman_collection.json`
2. `AuctionMicroservice_MS2_Local.postman_environment.json`

Before presenting, run:

```powershell
docker compose up -d --build
```

Select the `Auction Microservice MS2 Local` environment in Postman and run the collection from top to bottom.
The first request, `00 Setup / Reset Demo Variables`, creates a fresh `runId` and clears saved IDs.

All requests go through the API Gateway at `http://localhost:8080`. Do not call downstream services directly.

For the bash-script requirement, run:

```powershell
& 'C:\Program Files\Git\bin\bash.exe' scripts/system-integration-tests.sh
```

Expected final output:

```text
System integration script completed: 46 passed, 0 failed
```
