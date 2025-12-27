package com.kbulkup.asset.dto.cache;

import com.kbulkup.asset.domain.Composition;
import com.kbulkup.asset.domain.Snapshot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StaticAssetData implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Snapshot> snapshots;
    private Composition composition;
}
