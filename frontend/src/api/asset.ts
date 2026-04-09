import { http } from '@/api/http';
import type { PagedResult } from '@/types/api';
import type { AssetNetworkItem, AssetNetworkListParams } from '@/types/asset';

export const listAssetNetworksApi = (params: AssetNetworkListParams) => {
  return http.get<PagedResult<AssetNetworkItem>>('/api/asset/networks', { params });
};
