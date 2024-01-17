namespace AruppiApi.Database.Infraestructure;

public interface ISoftDeleted
{
    public bool IsDeleted { get; set; }
}
