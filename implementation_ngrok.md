# Add Ngrok to Docker Compose

The goal is to integrate `ngrok` into the [docker-compose.yml](file:///home/pran/anotherDrive/javaCodes/CPA/docker-compose.yml) file so that the application is automatically exposed to the internet when starting the stack.

## User Review Required
> [!IMPORTANT]
> **Ngrok Auth Token Required**: Ngrok requires an authentication token. You will need to provide this in an `.env` file or as an environment variable (`NGROK_AUTHTOKEN`) when running docker-compose.

## Proposed Changes

### Docker Configuration

#### [MODIFY] [docker-compose.yml](file:///home/pran/anotherDrive/javaCodes/CPA/docker-compose.yml)
- Add `ngrok` service.
- Configure it to tunnel to `app:8080`.
- Bind `NGROK_AUTHTOKEN` from environment.
- Expose port `4040` for the ngrok web inspection interface.

### Environment Setup

#### [NEW] [.env](file:///home/pran/anotherDrive/javaCodes/CPA/.env)
- Create a template `.env` file with `NGROK_AUTHTOKEN=your_token_here`.

## Verification Plan

### Manual Verification
1.  Run `sudo docker compose up --build`.
2.  Check logs for ngrok URL: `http://localhost:4040/api/tunnels` or in the container logs.
3.  Access the public ngrok URL and verify the app loads.
