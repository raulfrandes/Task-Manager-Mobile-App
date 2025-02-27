using Microsoft.IdentityModel.Tokens;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;
using System.Collections.Concurrent;
using System.IdentityModel.Tokens.Jwt;
using System.Net.WebSockets;
using System.Security.Claims;
using System.Text;

namespace Server.Config
{
    public class WebSocketManager
    {
        private static readonly ConcurrentDictionary<WebSocket, int> _sockets = new ConcurrentDictionary<WebSocket, int>();

        public static async Task AddSocket(WebSocket socket, string token, IConfiguration configuration)
        {
            var userId = int.Parse(ValidateTokenAndGetUserId(token, configuration));
            if (userId != null)
            {
                _sockets.TryAdd(socket, userId);
            }
            else
            {
                await socket.CloseAsync(WebSocketCloseStatus.PolicyViolation, "Invalid Token", CancellationToken.None);
            }
        }

        public static async Task BroadcastMessage(string eventType, object payload, int userId)
        {
            var message = new
            {
                eventType,
                payload
            };

            var settings = new JsonSerializerSettings
            {
                ContractResolver = new CamelCasePropertyNamesContractResolver()
            };

            var messageJson = JsonConvert.SerializeObject(message, settings);
            var buffer = Encoding.UTF8.GetBytes(messageJson);

            foreach (var (socket, socketUserID) in _sockets)
            {
                if (socket.State == WebSocketState.Open && userId == socketUserID)
                {
                    await socket.SendAsync(new ArraySegment<byte>(buffer, 0, buffer.Length), WebSocketMessageType.Text, true, CancellationToken.None);
                }
            }
        }

        public static async Task RemoveSocket(WebSocket socket)
        {
            _sockets.TryRemove(socket, out _);
            if (socket.State == WebSocketState.Open)
            {
                await socket.CloseAsync(WebSocketCloseStatus.NormalClosure, "Close by the WebSocketManager", CancellationToken.None);
            }
        }

        private static string ValidateTokenAndGetUserId(string token, IConfiguration configuration)
        {
            var tokenHandler = new JwtSecurityTokenHandler();
            var key = Encoding.UTF8.GetBytes(configuration["Jwt:Key"]);
            try
            {
                var claims = tokenHandler.ValidateToken(token, new TokenValidationParameters {
                    ValidateIssuerSigningKey = true,
                    IssuerSigningKey = new SymmetricSecurityKey(key),
                    ValidateIssuer = true,
                    ValidateAudience = true,
                    ValidIssuer = configuration["Jwt:Issuer"],
                    ValidAudience = configuration["Jwt:Audience"],
                    ClockSkew = TimeSpan.Zero
                }, out _);

                return claims.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            } catch
            {
                return null;
            }
        }
    }
}