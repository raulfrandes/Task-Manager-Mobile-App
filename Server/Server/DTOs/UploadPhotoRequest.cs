using Microsoft.AspNetCore.Mvc;

namespace Server.DTOs
{
    public class UploadPhotoRequest
    {
        [FromForm]
        public IFormFile File { get; set; }
    }
}
