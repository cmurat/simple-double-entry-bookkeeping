package per.cmurat.other.revolut.core.db;

public abstract class Entity {
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }
}
