package carfactory.model.details;

public abstract class Detail implements Numerable {

    public abstract String getName();

    @Override
    public String toString(){
        return getName() + "(id=" + getId() + ")";
    }

}
