package com.swyp14.phocamatch.favoritegroup.dto;

import com.swyp14.phocamatch.idolgroup.domain.IdolGroup;

public record NonFavoriteGroupItemResponse(
        Long groupId,
        String name,
        String groupImageUrl
) {
    public static NonFavoriteGroupItemResponse from(
            IdolGroup group
    ){
        return new NonFavoriteGroupItemResponse(
                group.getId(),
                group.getName(),
                group.getImageUrl()
        );
    }
}
