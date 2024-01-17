namespace BaseRestClient;

public class RequestConfig<TContext> : RequestConfig
{
    public TContext? Context { get; set; }
    new public Func<TContext?, HttpRequestMessage> MessageBuilder { get; set; } = null!;

    public override HttpRequestMessage BuildMessage()
    {
        // We build the messages that way to avoid creating "lambda instances"
        // that would happen if you use external data
        // that way we keep it static and prevent the performance hit that the other approach would have
        var message = MessageBuilder(Context);

        return message;
    }
}

public class RequestConfig
{
    public string? Endpoint { get; set; }
    public List<KeyValuePair<string, dynamic>>? QueryParams { get; set; }
    public Func<HttpRequestMessage> MessageBuilder { get; set; } = null!;

    public virtual HttpRequestMessage BuildMessage()
    {
        var message = MessageBuilder();

        return message;
    }
}
