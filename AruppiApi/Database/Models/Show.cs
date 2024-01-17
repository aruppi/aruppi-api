using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace AruppiApi.Database.Models;

[Table("Show")]
public partial class Show : BaseEntity
{
    [Key]
    [Column(TypeName = "GUID")]
    public override Guid Id { get; set; }

    public string? Url { get; set; }

    public string? Name { get; set; }

    public int? Runtime { get; set; }

    public int? AverageRuntime { get; set; }

    public string? Premiered { get; set; }

    public string? Ended { get; set; }

    public string? OfficialSite { get; set; }

    public int? Weight { get; set; }

    public string? Summary { get; set; }

    public int? Updated { get; set; }

    [InverseProperty("Show")]
    public virtual IList<ShowGenere> ShowGeneres { get; set; } = new List<ShowGenere>();

    [InverseProperty("Show")]
    public virtual IList<ShowLanguage> ShowLanguages { get; set; } = new List<ShowLanguage>();

    [InverseProperty("Show")]
    public virtual IList<ShowStatus> ShowStatuses { get; set; } = new List<ShowStatus>();
}
