using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace AruppiApi.Database.Models;

[Table("ShowLanguage")]
public partial class ShowLanguage : BaseEntity
{
    [Key]
    [Column(TypeName = "GUID")]
    public override Guid Id { get; set; }

    [Column(TypeName = "GUID")]
    public Guid ShowId { get; set; }

    [Column(TypeName = "GUID")]
    public Guid LanguageId { get; set; }

    [ForeignKey("LanguageId")]
    [InverseProperty("ShowLanguages")]
    public virtual Language Language { get; set; } = null!;

    [ForeignKey("ShowId")]
    [InverseProperty("ShowLanguages")]
    public virtual Show Show { get; set; } = null!;
}
