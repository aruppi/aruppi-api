using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace AruppiApi.Database.Models;

[Table("Genere")]
public partial class Genere : BaseEntity
{
    [Key]
    [Column(TypeName = "GUID")]
    public override Guid Id { get; set; }

    public string Name { get; set; } = null!;

    [InverseProperty("Genere")]
    public virtual IList<ShowGenere> ShowGeneres { get; set; } = new List<ShowGenere>();
}
