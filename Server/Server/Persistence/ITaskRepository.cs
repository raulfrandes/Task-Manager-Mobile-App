using Server.DTOs;

namespace Server.Persistence
{
    public interface ITaskRepository
    {
        Task<List<Entities.Task>> GetAllTasksAsync();
        Task<Entities.Task?> GetTaskAsync(int id);
        Task<Entities.Task> CreateAsync(Entities.Task entity);
        Task<Entities.Task?> UpdateAsync(Entities.Task entity);
        Task<Entities.Task?> DeleteAsync(int id);
        Task<List<Entities.Task>> GetTasksByUserIdAsync(int userId);
        Task<(List<Entities.Task>, int)> GetTasksByUserIdAsync(int userId, int skip, int take, string? searchQuery = null, bool? completed = null);
    }
}
