using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace AruppiApi.Database.Models;

[Table("Status")]
public partial class Status : BaseEntity
{
    [Key]
    [Column(TypeName = "GUID")]
    public override Guid Id { get; set; }

    public string? Name { get; set; }

    [InverseProperty("Status")]
    public virtual IList<ShowStatus> ShowStatuses { get; set; } = new List<ShowStatus>();
}
