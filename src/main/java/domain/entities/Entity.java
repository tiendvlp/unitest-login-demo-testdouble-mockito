package domain.entities;

public abstract class Entity {
    public String id;

    protected Entity()
    {

    }

    protected Entity(String id)
    {
        id = id;
    }

    public boolean equals (Object obj) {
        if (!(obj instanceof Entity)) return false;
        if (id.equals(((Entity) obj).id)) return true;
        if (obj == this) return true;
        return false;
    }
}
