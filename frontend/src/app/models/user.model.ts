export interface User{
  email: string;
  username: string;
  guest: boolean;
}

export interface LoginRequest{
  email: string;
  password: string;
}

export interface RegisterRequest{
  email: string;
  username: string;
  password: string;
}

export interface AuthResponse{
  token: string;
  user: User;
}
