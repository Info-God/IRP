import { apiRequest } from "@/lib/apiClient";
import { USE_MOCKS } from "@/lib/config";
import { delay } from "@/lib/delay";
import { mockUser } from "@/mocks/mockData";
import type { AuthResponse } from "@/types";

export interface LoginPayload {
  email: string;
  password: string;
}

export async function login(payload: LoginPayload): Promise<AuthResponse> {
  if (USE_MOCKS) {
    // Any credentials are accepted in mock mode - this is a demo, not real auth.
    void payload;
    return delay(
      {
        accessToken: "mock-token",
        tokenType: "Bearer",
        expiresInSeconds: 3600,
        user: mockUser,
      },
      400,
    );
  }
  return apiRequest<AuthResponse>("/api/v1/auth/login", { method: "POST", body: payload });
}
