using AutoMapper;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Cors;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Server.Config;
using Server.DTOs;
using Server.Persistence;
using System.Security.Claims;

namespace Server.Controllers
{
    [Route("api/tasks")]
    [ApiController]
    [Authorize]
    public class TaskController : ControllerBase
    {
        private readonly ITaskRepository _taskRepository;
        private readonly IMapper _mapper;

        public TaskController(ITaskRepository taskRepository, IMapper mapper)
        {
            _taskRepository = taskRepository;
            _mapper = mapper;
        }

        [HttpGet]
        public async Task<IActionResult> GetAll(
            [FromQuery] int skip = 0, 
            [FromQuery] int take = 10,
            [FromQuery] string? searchQuery = null,
            [FromQuery] bool? completed = null)
        {
            var token = Request.Headers["Authorization"].ToString();
            Console.WriteLine($"Received Token: {token}");

            var userId = int.Parse(User.FindFirst(ClaimTypes.NameIdentifier)?.Value ?? "0");

            var (tasks, totalTasks) = await _taskRepository.GetTasksByUserIdAsync(userId, skip, take, searchQuery, completed);

            var tasksDto = _mapper.Map<List<TaskDto>>(tasks);

            return Ok(new { tasks = tasksDto, totalTasks });
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> GetTask([FromRoute] int id)
        {
            var userId = int.Parse(User.FindFirst(ClaimTypes.NameIdentifier)?.Value ?? "0");
            var task = await _taskRepository.GetTaskAsync(id);

            if (task != null)
            {
                if (task.UserID == userId)
                {
                    var taskDto = _mapper.Map<TaskDto>(task);

                    return Ok(taskDto);
                }
                else
                {
                    return Forbid();
                }
            }
            else
            {
                return NotFound();
            }
        }

        [HttpPost]
        public async Task<IActionResult> Create([FromBody] CreateTaskDto createTaskDto)
        {
            var userId = int.Parse(User.FindFirst(ClaimTypes.NameIdentifier)?.Value ?? "0");
            var taskModel = _mapper.Map<Entities.Task>(createTaskDto);
            taskModel.UserID = userId;
            await _taskRepository.CreateAsync(taskModel);

            var taskDto = _mapper.Map<TaskDto>(taskModel);
            await Server.Config.WebSocketManager.BroadcastMessage("TaskAdded", new { task = taskDto }, userId);

            return Ok(taskModel);
        }

        [HttpPut("{id}")]
        public async Task<IActionResult> Update([FromRoute] int id, [FromBody] UpdateTaskDto updateTaskDto)
        {
            var userId = int.Parse(User.FindFirst(ClaimTypes.NameIdentifier)?.Value ?? "0");
            var task = await _taskRepository.GetTaskAsync(id);

            if (task == null || task.UserID != userId)
            {
                return Unauthorized();
            }

            var taskModel = await _taskRepository.UpdateAsync(new Entities.Task
            {
                Id = id,
                Title = updateTaskDto.Title,
                Description = updateTaskDto.Description,
                DueDate = updateTaskDto.DueDate,
                Priority = updateTaskDto.Priority,
                Completed = updateTaskDto.Completed,
                UserID = userId,
                PhotoUrl = updateTaskDto.PhotoUrl,
                Latitude = updateTaskDto.Latitude,
                Longitude = updateTaskDto.Longitude
            });

            if (taskModel == null)
            {
                return NotFound();
            }

            var taskDto = _mapper.Map<TaskDto>(taskModel);
            await Server.Config.WebSocketManager.BroadcastMessage("TaskUpdated", new { task = taskDto }, userId);

            return Ok(taskDto);
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete([FromRoute] int id)
        {
            var userId = int.Parse(User.FindFirst(ClaimTypes.NameIdentifier)?.Value ?? "0");
            var taskModel = await _taskRepository.DeleteAsync(id);

            if (taskModel == null && userId != taskModel.UserID)
            {
                return Forbid();
            }

            var taskDto = _mapper.Map<TaskDto>(taskModel);
            await Server.Config.WebSocketManager.BroadcastMessage("TaskDeleted", new { task = taskDto }, userId);

            return NoContent();
        }
    }
}
