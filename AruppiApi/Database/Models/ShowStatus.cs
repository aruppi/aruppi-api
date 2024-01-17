using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace AruppiApi.Database.Models;

[Table("ShowStatus")]
public partial class ShowStatus : BaseEntity
{
    [Key]
    [Column(TypeName = "GUID")]
    public override Guid Id { get; set; }

    [Column(TypeName = "GUID")]
    public Guid ShowId { get; set; }

    [Column(TypeName = "GUID")]
    public Guid StatusId { get; set; }

    [ForeignKey("ShowId")]
    [InverseProperty("ShowStatuses")]
    public virtual Show Show { get; set; } = null!;

    [ForeignKey("StatusId")]
    [InverseProperty("ShowStatuses")]
    public virtual Status Status { get; set; } = null!;
}
