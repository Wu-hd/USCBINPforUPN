export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
  timestamp: string;
}

export interface PagedResult<T> {
  page: {
    pageNum: number;
    pageSize: number;
    total: number;
  };
  list: T[];
}

export interface UserInfo {
  username: string;
}
