using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace AruppiApi.Database.Models;

[Table("ShowGenere")]
public partial class ShowGenere : BaseEntity
{
    [Key]
    [Column(TypeName = "GUID")]
    public override Guid Id { get; set; }

    [Column(TypeName = "GUID")]
    public Guid ShowId { get; set; }

    [Column(TypeName = "GUID")]
    public Guid GenereId { get; set; }

    [ForeignKey("GenereId")]
    [InverseProperty("ShowGeneres")]
    public virtual Genere Genere { get; set; } = null!;

    [ForeignKey("ShowId")]
    [InverseProperty("ShowGeneres")]
    public virtual Show Show { get; set; } = null!;
}
