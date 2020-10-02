package carfactory.model;

public record State(
    Integer bodyStorageSize,
    Integer engineStorageSize,
    Integer accessoriesStorageSize,
    Integer carStorageSize,
    Integer bodiesTotalMade,
    Integer enginesTotalMade,
    Integer accessoriesTotalMade,
    Integer carTotalMade,
    Integer inProgress,
    Integer totalSold,
    Integer salesInProgress
){}
