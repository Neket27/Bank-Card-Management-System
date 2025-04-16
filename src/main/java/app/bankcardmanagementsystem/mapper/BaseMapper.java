package app.bankcardmanagementsystem.mapper;

import org.mapstruct.MappingTarget;

import java.util.List;

public interface BaseMapper<E,D> {

    E toEntity(D d);
    D toDto(E e);
    E updateEntity(@MappingTarget E e, E updated);
    List<E> toEntityList(List<D> d);
    List<D> toDtoList(List<E> e);
}
