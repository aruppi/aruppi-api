using System;
using System.Collections.Generic;
using AruppiApi.Database.Models;
using Microsoft.EntityFrameworkCore;

namespace AruppiApi.Database;

public partial class DatabaseContext : DbContext
{
    public DatabaseContext(DbContextOptions<DatabaseContext> options)
        : base(options)
    {
    }

    public virtual DbSet<Genere> Generes { get; set; }

    public virtual DbSet<Language> Languages { get; set; }

    public virtual DbSet<Show> Shows { get; set; }

    public virtual DbSet<ShowGenere> ShowGeneres { get; set; }

    public virtual DbSet<ShowLanguage> ShowLanguages { get; set; }

    public virtual DbSet<ShowStatus> ShowStatuses { get; set; }

    public virtual DbSet<Status> Statuses { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Genere>(entity =>
        {
            entity.Property(e => e.Id).ValueGeneratedNever();
        });

        modelBuilder.Entity<Language>(entity =>
        {
            entity.Property(e => e.Id).ValueGeneratedNever();
        });

        modelBuilder.Entity<Show>(entity =>
        {
            entity.Property(e => e.Id).ValueGeneratedNever();
        });

        modelBuilder.Entity<ShowGenere>(entity =>
        {
            entity.Property(e => e.Id).ValueGeneratedNever();
        });

        modelBuilder.Entity<ShowLanguage>(entity =>
        {
            entity.Property(e => e.Id).ValueGeneratedNever();
        });

        modelBuilder.Entity<ShowStatus>(entity =>
        {
            entity.Property(e => e.Id).ValueGeneratedNever();
        });

        modelBuilder.Entity<Status>(entity =>
        {
            entity.Property(e => e.Id).ValueGeneratedNever();
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
