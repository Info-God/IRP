from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    irp_core_base_url: str = "http://localhost:8080"
    irp_core_api_key: str = ""

    internal_token: str = "dev-only-internal-token-change-me"

    groq_api_key: str = ""
    groq_model: str = "llama-3.3-70b-versatile"

    max_tool_iterations: int = 8


settings = Settings()
