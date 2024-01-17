using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace AruppiApi.Database.Models;

[Table("Language")]
public partial class Language : BaseEntity
{
    [Key]
    [Column(TypeName = "GUID")]
    public override Guid Id { get; set; }

    public string? Name { get; set; }

    [InverseProperty("Language")]
    public virtual IList<ShowLanguage> ShowLanguages { get; set; } = new List<ShowLanguage>();
}
