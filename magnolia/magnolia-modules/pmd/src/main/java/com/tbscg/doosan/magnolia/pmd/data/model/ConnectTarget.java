package com.tbscg.doosan.magnolia.pmd.data.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class ConnectTarget {

    private long id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectTarget that = (ConnectTarget) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public abstract String getLabel();

    public String getType() {
        return getClass().getSimpleName();
    }

    public abstract boolean hasChildren();

    @Override
    public String toString() {
        return getType() + "{" + "id=" + id + '}';
    }
}
