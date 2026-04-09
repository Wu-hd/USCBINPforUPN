export interface AssetNetworkItem {
  id: number;
  networkCode: string;
  networkName: string;
  networkType: string;
  regionCode: string;
  serviceStatus: number;
}

export interface AssetNetworkListParams {
  pageNum: number;
  pageSize: number;
  regionCode?: string;
}
