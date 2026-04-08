package com.uscbinp.domain.service.asset;

import java.util.List;

public interface AssetNetworkService {

    NetworkItem create(NetworkUpsertCommand command);

    NetworkItem update(Long id, NetworkUpsertCommand command);

    void delete(Long id);

    NetworkItem get(Long id);

    PageResult list(int pageNum, int pageSize, String regionCode, boolean fullAccess);

    boolean exists(Long id);

    record NetworkUpsertCommand(String networkCode,
                                String networkName,
                                String networkType,
                                String regionCode,
                                Integer serviceStatus,
                                String levelCode) {
    }

    record NetworkItem(Long id,
                       String networkCode,
                       String networkName,
                       String networkType,
                       String regionCode,
                       Integer serviceStatus) {
    }

    record PageInfo(int pageNum, int pageSize, long total) {
    }

    record PageResult(PageInfo page, List<NetworkItem> list) {
    }
}
