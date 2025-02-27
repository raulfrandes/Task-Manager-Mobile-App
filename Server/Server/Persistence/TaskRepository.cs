
using Microsoft.EntityFrameworkCore;
using Server.DTOs;

namespace Server.Persistence
{
    public class TaskRepository : ITaskRepository
    {
        private readonly ApplicationDbContext _context;
        public TaskRepository(ApplicationDbContext context)
        {
            _context = context;
        }

        public async Task<Entities.Task> CreateAsync(Entities.Task entity)
        {
            await _context.AddAsync(entity);
            await _context.SaveChangesAsync();
            return entity;
        }

        public async Task<Entities.Task?> DeleteAsync(int id)
        {
            var taskModel = await _context.Tasks.FirstOrDefaultAsync(x => x.Id == id);

            if (taskModel == null)
            {
                return null;
            }

            _context.Tasks.Remove(taskModel);
            await _context.SaveChangesAsync();
            return taskModel;
        }

        public async Task<List<Entities.Task>> GetAllTasksAsync()
        {
            return await _context.Tasks.ToListAsync();
        }

        public async Task<Entities.Task?> GetTaskAsync(int id)
        {
            return await _context.Tasks.FindAsync(id);
        }

        public async Task<List<Entities.Task>> GetTasksByUserIdAsync(int userId)
        {
            return await _context.Tasks.Where(t => t.UserID == userId).ToListAsync();
        }

        public async Task<(List<Entities.Task>, int)> GetTasksByUserIdAsync(int userId, int skip, int take, string? searchQuery = null, bool? completed = null)
        {
            var query = _context.Tasks
                .Where(t => t.UserID == userId);

            if (!string.IsNullOrEmpty(searchQuery))
            {
                query = query.Where(t => t.Title.Contains(searchQuery) || t.Description.Contains(searchQuery));
            }

            if (completed.HasValue)
            {
                query = query.Where(t => t.Completed == completed.Value);
            }

            var totalTasks = await query.CountAsync();

            var tasks = await query
                .OrderBy(t => t.Id)
                .Skip(skip)
                .Take(take)
                .ToListAsync();

            return (tasks, totalTasks);
        }

        public async Task<Entities.Task?> UpdateAsync(Entities.Task entity)
        {
            var existingEntity = await _context.Tasks.FirstOrDefaultAsync(x => x.Id == entity.Id);
            if (existingEntity == null)
            {
                return null;
            }

            existingEntity.Id = entity.Id;
            existingEntity.Title = entity.Title;
            existingEntity.Description = entity.Description;
            existingEntity.DueDate = entity.DueDate;
            existingEntity.Priority = entity.Priority;
            existingEntity.Completed = entity.Completed;
            existingEntity.UserID = entity.UserID;
            existingEntity.PhotoUrl = entity.PhotoUrl;
            existingEntity.Latitude = entity.Latitude;
            existingEntity.Longitude = entity.Longitude;

            await _context.SaveChangesAsync();
            return existingEntity;
        }
    }
}
