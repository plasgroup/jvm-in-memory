using System;
using System.Collections.Generic;
using System.Linq;

namespace ConfigFileGenerator.configurator
{
    public static class Ext
    {
        public static void PrintToConsole(this object obj)
        {
            Console.WriteLine(obj);
        }

        public static void PrintEnumerableToConsole<T>(this IEnumerable<T> e)
        {
            var enumerable = e as T[] ?? e.ToArray();
            if(enumerable.Length == 0) "[]".PrintToConsole();
            if(enumerable.Length == 1) $"[{enumerable.First()}]".PrintToConsole();
            (enumerable.Aggregate("[", (a, b) => a + ", " + b) + "]").PrintToConsole();
        }

        public static string GetCollectionString<T>(this IEnumerable<T> e)
        {
            var enumerable = e as T[] ?? e.ToArray();
            if(enumerable.Length == 0) "[]".PrintToConsole();
            if (enumerable.Length == 1) return $"[{enumerable.First()}]";
            return enumerable.Aggregate("[", (a, b) => a + ", " + b) + "]";
        }
        public static void PrintEnumerableToConsole2Dim<T>(this IEnumerable<IEnumerable<T>> e)
        {
            var enumerable = e as T[][] ?? e.ToArray();
            if(enumerable.Length == 0) "[]".PrintToConsole();
            if(enumerable.Length == 1) $"[{enumerable.First()}]".PrintToConsole();
            (enumerable.Aggregate("[", (a, b) => a + ", " + GetCollectionString(b) + "]")).PrintToConsole();
        }
        
    }
}