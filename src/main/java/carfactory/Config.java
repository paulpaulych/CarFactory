package carfactory;

import java.util.Properties;

/**
 * represents immutable execution configurations which are loaded before on app start
 */
public record Config(
    Integer dealersCount,
    Integer accessoriesSuppliersCount,
    Integer optimalCarStorageSize,
    Integer carStorageMaxSize,
    Integer bodyStorageMaxSize,
    Integer engineStorageMaxSize,
    Integer accessoriesStorageMaxSize
){

    private  static class Consts {
        public static final String DEALERS_COUNT = "dealers_count";
        public static final String ACCESSORIES_SUPPLIERS_COUNT = "accessories_suppliers_count";
        public static final String CAR_STORAGE_MAX_SIZE = "car_storage_max_size";
        public static final String BODY_STORAGE_MAX_SIZE = "body_storage_max_size";
        public static final String ENGINE_STORAGE_MAX_SIZE = "engine_storage_max_size";
        public static final String ACCESSORIES_STORAGE_MAX_SIZE = "accessories_storage_max_size";
        public static final String OPTIMAL_CAR_STORAGE_SIZE = "optimal_car_storage_size";
    }

    public static Config from(Properties properties){
        return new Config(
                Integer.valueOf(properties.getProperty(Consts.DEALERS_COUNT)),
                Integer.valueOf(properties.getProperty(Consts.ACCESSORIES_SUPPLIERS_COUNT)),
                Integer.valueOf(properties.getProperty(Consts.OPTIMAL_CAR_STORAGE_SIZE)),
                Integer.valueOf(properties.getProperty(Consts.CAR_STORAGE_MAX_SIZE)),
                Integer.valueOf(properties.getProperty(Consts.BODY_STORAGE_MAX_SIZE)),
                Integer.valueOf(properties.getProperty(Consts.ENGINE_STORAGE_MAX_SIZE)),
                Integer.valueOf(properties.getProperty(Consts.ACCESSORIES_STORAGE_MAX_SIZE))
        );
    }

}
