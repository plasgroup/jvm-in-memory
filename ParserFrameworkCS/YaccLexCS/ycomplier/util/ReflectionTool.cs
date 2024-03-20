using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using YaccLexCS.ycomplier.attribution;

namespace YaccLexCS.ycomplier.util
{
    public static class ReflectionTool
    {
        public static IEnumerable<Type> ScanConfigurationClass<T>(IEnumerable<string> packetName)
        {
            
            var tokenConfig = 
                (Assembly.GetExecutingAssembly()
                    .GetTypes()
                    .Where(t => t.IsClass && packetName.Any(pName => t.Namespace == pName || (t.Namespace?.StartsWith(pName + ".") ?? false)) &&
                                t.GetCustomAttribute(typeof(T)) != null)).ToList();
            tokenConfig.ForEach(t => Console.WriteLine(t.Name));
            return tokenConfig;
        }
        
        public static IEnumerable<(T tokenDef, MethodInfo methodInfo)> GetAllDefinitions<T>(IEnumerable<Type> types) where T : Attribute
        {
            
            var methods = types.SelectMany(e => e.GetMethods());
            
            var result = methods
                .Where(m => m.GetCustomAttributes(typeof(T)).Any() && m.IsStatic)
                .SelectMany(m => m.GetCustomAttributes<T>().Select(attr => (attr, m)));
            
            return result;
        }
    }
}