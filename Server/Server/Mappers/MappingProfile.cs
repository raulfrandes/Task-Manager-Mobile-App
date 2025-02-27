using AutoMapper;
using Server.DTOs;
using Server.Entities;

namespace Server.Mappers
{
    public class MappingProfile : Profile
    {
        public MappingProfile()
        {
            CreateMap<Entities.Task, TaskDto>().ReverseMap();
            CreateMap<Entities.Task, CreateTaskDto>().ReverseMap();
            CreateMap<Entities.Task, UpdateTaskDto>().ReverseMap();
            CreateMap<User, RegisterDto>().ReverseMap();
            CreateMap<User, LoginDto>().ReverseMap();
            CreateMap<User, UserDto>().ReverseMap();
        }
    }
}
