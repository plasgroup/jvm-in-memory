#define DEBUGMODE
using System;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Linq;
using System.Text.RegularExpressions;
using YaccLexCS.ycomplier.util;
namespace YaccLexCS.ycomplier
{
      public static class FunctionExt
    {

        public static T? AggregateOneOrMore<T>(this IEnumerable<T> enumerable, Func<T, T, T> func)
        {
            if (!enumerable.Any())
                return default;
            return enumerable.Count() == 1 ? enumerable.First() : enumerable.Aggregate(func);
        }
        public static void AddRange<T>(this HashSet<T> target, IEnumerable<T> data)
        {
            foreach (var e in data)
            {
                target.Add(e);
            }
        }
        public static void PrintToConsole(this object obj)
        {
            Console.WriteLine(obj.ToString());
        }

        public static void DebugOutPut(this object obj)
        {
#if DEBUGMODE
            Console.WriteLine(obj.ToString());
#endif
        }

        public static void ElementInvoke<T>(this IEnumerable<T> list, Action<T> action)
        {
            Action a;
            foreach (var e in list)
            {
                action.Invoke(e);
            }
        }
        public static void ElementInvoke<T>(this IEnumerable<T> list, Action<T, int> action)
        {
            var enumerable = list as T[] ?? list.ToArray();

            for (var i = 0; i < enumerable.Length; i++)
            {
                action.Invoke(enumerable[i], i);
            }
        }
        public static void EnumerableConditionInvoke<T>(this IEnumerable<T> list,Func<T ,bool> condition, Action<T> action)
        {
            foreach (var e in list)
            {
                if(condition.Invoke(e))
                    action.Invoke(e);
            }
        }

        public static IEnumerable<T2> WindowSelect<T, T2>(this IEnumerable<T> list, int wSize, Func<IEnumerable<T>, int, T2> selFunc)
        {
            var l = list.ToList();

            var right = wSize - 1;
            var window = new Queue<T>();
            for (var i = 0; i <= right; i++)
            {
                window.Enqueue(l[i]);
            }

            var left = 0;
            do
            { 
                var tmp = window.ToList();
                var ans = selFunc.Invoke(tmp, left++);
                window.Dequeue();
                if(right + 1 < l.Count)
                    window.Enqueue(l[right + 1]);
                right++;
                yield return ans;
            }while(right< l.Count);
            
            
        }
        public static IEnumerable<T2> WindowSelect<T, T2>(this IEnumerable<T> list, int wSize, Func<IEnumerable<T>, T2> selFunc, bool equalWidth = false)
        {
            var enumerable = list as T[] ?? list.ToArray();
            var l = enumerable.ToList();

            var right = wSize - 1;
            var window = new Queue<T>();
            for (var i = 0; i <= right; i++)
            {
                window.Enqueue(l[i]);
            }

            if (!equalWidth)
            {
                do
                { 
                    var tmp = window.ToList();
                    var ans = selFunc.Invoke(tmp);
                    window.Dequeue();
                    if(right + 1 < l.Count)
                        window.Enqueue(l[right + 1]);
                    right++;
                    yield return ans;
                }while(right< l.Count);
            }
            else
            {
                var t = Enumerable.Range(0, enumerable.Length).Select(e =>
                {
                    var left = e;
                    var r = e + wSize;
                    if (r >= enumerable.Length)
                    {
                        left = left - (r - enumerable.Length) - 1;
                    }
                    return selFunc.Invoke(enumerable.Skip(left).Take(wSize).ToList());
                });
                foreach (var w in t)
                {
                    yield return w;
                }
            }



        }
        public static IEnumerable<T> SelectByIndexes<T>(this IEnumerable<T> list, params int[] indexes)
        {
            var l = list.ToList();
            foreach (var i in indexes)
            {
                if (i < l.Count)
                {
                    yield return l[i];
                }
            }
        }
   
     

      
        public static IEnumerable<T> SelectByIndexes<T>(this IEnumerable<T> enumerable,IEnumerable<int> indexes)
        {
            var list = enumerable.ToArray();
            var ints = indexes as int[] ?? indexes.ToArray();
            var indexList = ints.ToArray();
            for (var i = 0; i < ints.Length; i++)
            {
                if (indexList[i] < list.Length)
                    yield return list[indexList[i]];
                else
                    yield return default;
            }
        }

      
        public static IEnumerable<int> FindAndGetIndexes<T>(this IEnumerable<T> enumerable, Func<T,bool> condition)
        {
            var bArr = enumerable.ConditionFindWithBoolResult(condition);
            return bArr.ConditionSelect(((b, _) => b)
                , (_, i) => i
            );
        }
        
        
        //比aggregate高级的东西

        public static Key Sum<T, Key>(this IEnumerable<T> enumerable, Func<T, Key> keySelector,
            Func<Key, Key, Key> addStrategy)
        {
            var list = enumerable.ToList();
            switch (list.Count)
            {
                case 0:
                    return default;
                case 1:
                    return keySelector.Invoke(list[0]);
            }

            
            var cur = keySelector.Invoke(list[0]);
            
            for (var i = 1; i < list.Count; i++)
            {
                cur = addStrategy.Invoke(cur, keySelector.Invoke(list[i]));
            }
           
            return cur;
        }
      
        public static void PrintEnumerationToConsole<T>(this IEnumerable<T> list)
        {

            Console.WriteLine(list.ToEnumerationString());

        }
        public static string ToEnumerationString<T>(this IEnumerable<T> list)
        {
            if (!list.Any())
                return "[]";
            var res = list.Aggregate("[", (current, e) => current + (e + ","));
            
            res = res.Substring(0, res.Length - 1) + "]";
            return res;
        }
        public static (int, TSource) ArgMax<TSource, TKey>(this IEnumerable<TSource> enumerable,
            Func<TSource, TKey> keySelector,
            IComparer<TKey> comparer = null)
        {
            if (comparer != null)
                return enumerable.ArgMin(keySelector,
                    new CustomerComparer<TKey>((t1, t2) => -comparer.Compare(t1, t2)));
            {
                if (typeof(TKey).GetInterface("IComparable") != null)
                {
                    comparer = new CustomerComparer<TKey>((t1, t2) => ((IComparable) t1).CompareTo(t2));
                }
                else
                {
                    throw new ArithmeticException();
                }
            }
            return enumerable.ArgMin(keySelector, new CustomerComparer<TKey>((t1, t2) => -comparer.Compare(t1, t2)));
        }
        public static Matrix<TSource> ToMatrix2D<TSource>(this IEnumerable<IEnumerable<TSource>> source, bool isValueMat = false,bool reverse = false)
        {
            var enumerable = source as IEnumerable<TSource>[] ?? source.ToArray();
            var t = enumerable.ToArray();
            var r = t.Length;
            if (r == 0)
                return isValueMat? new ValueMatrix<TSource>(0, 0) : new Matrix<TSource>(0, 0);
            var c = t[0].Count();
            var ans = enumerable.SelectMany(e => e).ToMatrix(r, c, isValueMat);
            return reverse ? ans._T() : ans;
        }
        public static Matrix<TSource> ToMatrix<TSource>(this IEnumerable<TSource> sources, bool inRow = true)
        {
            var tSources = sources as TSource[] ?? sources.ToArray();
            var c = tSources.Length;
            return tSources.ToMatrix(inRow ? 1 : c, inRow ? c : 1);
        }
        
        public static IEnumerable<string> FindAll(this string str, Regex regex)
        {
            var m = regex.Matches(str);
            return regex.Matches(str).Select(e => e.Value);

        }
        public static IEnumerable<(int, T)> FindAll<T>(this IEnumerable<T> enumerable, Func<T, bool> condition)
        {
            var t = enumerable.ToArray();
            for(var i = 0; i < t.Length; i++)
            {
                if (condition.Invoke(t[i]))
                {
                    yield return (i, t[i]);
                }
            }
        }
        public static IEnumerable<(int, T)> FindAll<T>(this IEnumerable<T> enumerable, Func<T, int, bool> condition)
        {
            var t = enumerable.ToArray();
            for(var i = 0; i < t.Length; i++)
            {
                if (condition.Invoke(t[i], i))
                {
                    yield return (i, t[i]);
                }
            }
        }
        public static IEnumerable<TResult> FindAll<T, TResult>(this IEnumerable<T> enumerable, Func<T, int, bool> condition, Func<T, int, TResult> sel)
        {
            var t = enumerable.ToArray();
            for(var i = 0; i < t.Length; i++)
            {
                if (condition.Invoke(t[i], i))
                {
                    yield return sel(t[i], i);
                }
            }
        }


        public static bool HasImplementedRawGeneric([NotNull] this Type type, [NotNull] Type generic)
        {
            if (type == null) throw new ArgumentNullException(nameof(type));
            if (generic == null) throw new ArgumentNullException(nameof(generic));

            // 测试接口。
            var isTheRawGenericType = type.GetInterfaces().Any(IsTheRawGenericType);
            if (isTheRawGenericType) return true;

            // 测试类型。
            while (type != null && type != typeof(object))
            {
                
                if(type.IsGenericType)
                    (type.GetGenericTypeDefinition().GenericTypeArguments).PrintEnumerationToConsole();
                isTheRawGenericType = IsTheRawGenericType(type);
                if (isTheRawGenericType) return true;
                type = type.BaseType;
            }

            // 没有找到任何匹配的接口或类型。
            return false;

            // 测试某个类型是否是指定的原始接口。
            bool IsTheRawGenericType(Type test)
                => generic == (test.IsGenericType ? test.GetGenericTypeDefinition() : test);
        }
        public static Matrix<TSource> ToMatrix<TSource>(this IEnumerable<TSource> sources, int rows, int cols, bool isValueMat = false)
        {
            var tSources = sources as TSource[] ?? sources.ToArray();
            if(tSources.Length != rows * cols)
            {
                var mat = isValueMat ? new ValueMatrix<TSource>(rows, cols):new Matrix<TSource>(rows, cols);
                for (var i = 0; i < rows; i++)
                {
                    for (var j = 0; j < cols; j++)
                    {
                        var index = i * rows + j;
                        if (index >= tSources.Length)
                            return mat;
                        mat[i, j] = tSources[index];
                    }
                }
            }
            var matrix = new Matrix<TSource>(CollectionHelper.CreateTwoDimensionList(
                tSources.ToArray(),
                rows,cols
            ));
            return matrix;
        }

        public static DataFrame ToDataFrame<T>(this IEnumerable<IEnumerable<T>> enumerable, string[] cols = null)
        {
            var ses = enumerable as IEnumerable<T>[] ?? enumerable.ToArray();
            var maxWidth = ses.Max(e => e.Count());
            var n = ses.Length;
            var df = new DataFrame(cols ?? Enumerable.Range(0, n).Select(e => e + ""));
            foreach (var s in ses)
            {
                df.AddRow(s.ToArray());
            }
            return df;
        }
        public static (int, TSource) ArgMin<TSource, TKey>(this IEnumerable<TSource> enumerable,  Func<TSource, TKey> keySelector,
            IComparer<TKey> comparer = null)
        {
            var list = enumerable.ToArray();
            switch (list.Length)
            {
                case 0:
                    return (-1, default);
                case 1:
                    return (0, list[0]);
            }

            if (comparer == null)
            {
                if (typeof(TKey).GetInterface("IComparable") != null)
                {
                    comparer = new CustomerComparer<TKey>((t1, t2) => ((IComparable) t1).CompareTo(t2));
                }
                else
                {
                    throw new ArithmeticException("not comparable");
                }
            }
            var c = keySelector.Invoke(list[0]);
            var mIndex = 0;
            for (var i = 1; i < list.Length; i++)
            {
                var tmp = keySelector.Invoke(list[i]);
                if (comparer.Compare(tmp, c) >= 0) continue;
                c = tmp;
                mIndex = i;

            }

            return (mIndex, list[mIndex]);
        }
        
        public static IEnumerable<bool> ConditionFindWithBoolResult<T>(this IEnumerable<T> list, Func<T,bool> condition)
        {
            var ans = list.Select(condition.Invoke).ToList();
            return ans;
        }
        public static IEnumerable<bool> ConditionFindWithBoolResult<T>(this IEnumerable<T> list, Func<T, int, bool> condition)
        {
            var ans = list.Select(condition.Invoke).ToList();
            return ans;
        }
        public static IEnumerable<T2> ConditionSelect<T, T2>(this IEnumerable<T> enumerable, IEnumerable<bool> conditionBools, Func<T, T2> selectFunc = null)
        {
            var con = conditionBools.ToArray();
            selectFunc ??= t => (T2) (object) t;
            
            var ts = enumerable as T[] ?? enumerable.ToArray();
            for (var i = 0; i < ts.Length; i++)
            {
                if (con[i])
                    yield return selectFunc(ts[i]);
            }
        }
        
        public static IEnumerable<T2> ConditionSelect<T, T2>(this IEnumerable<T> enumerable, IEnumerable<bool> conditionBools, Func<T,int, T2> selectFunc = null)
        {
            var con = conditionBools.ToArray();
            selectFunc ??= (i, t) => (T2) ((i, t) as object);
            
            var ts = enumerable as T[] ?? enumerable.ToArray();
            for (var i = 0; i < ts.Length; i++)
            {
                if (con[i])
                    yield return selectFunc(ts[i], i);
            }
        }
        public static IEnumerable<T> ConditionSelect<T>(this IEnumerable<T> enumerable, Func<T, bool> condition)
        {
            var ts = enumerable as T[] ?? enumerable.ToArray();
            return ts.ConditionSelect((t, _) => condition.Invoke(t), (t, _) => t);
        }
        public static IEnumerable<T> ConditionSelect<T>(this IEnumerable<T> enumerable, IEnumerable<bool> condition)
        {
            var ts = enumerable as T[] ?? enumerable.ToArray();
            return ts.ConditionSelect(condition, t => t);
        }
        public static IEnumerable<T2> ConditionSelect<T, T2>(this IEnumerable<T> enumerable, Func<T, int, bool> condition, Func<T,int, T2> selectFunc = null)
        {
            var ts = enumerable as T[] ?? enumerable.ToArray();
            return ts.ConditionSelect(ts.ConditionFindWithBoolResult(condition), selectFunc);
        }
        
        public static IEnumerable<T2> ConditionSelect<T, T2>(this IEnumerable<T> enumerable, Func<T, bool> condition, Func<T, T2> selectFunc = null)
        {
            var ts = enumerable as T[] ?? enumerable.ToArray();
            return ts.ConditionSelect(ts.ConditionFindWithBoolResult(condition), selectFunc);
        }
        public static IEnumerable<T2> ConditionSelect<T, T2>(this IEnumerable<T> enumerable, Func<T, bool> condition, Func<T,int, T2> selectFunc)
        {
            var ts = enumerable as T[] ?? enumerable.ToArray();
            return ts.ConditionSelect(ts.ConditionFindWithBoolResult(condition), selectFunc);
        }
        
        public static void PrintCollectionToConsole<T>(this IEnumerable<T> enumerable)
        {
            var str = enumerable.Aggregate("[", (current, e) => current + (e + ","));
            str = str.Substring(0, str.Length - 1) + "]";
            Console.WriteLine(str);
        }
        public static void DebugPrintCollectionToConsole<T>(this IEnumerable<T> enumerable)
        {
#if DEBUGMODE
            var str = enumerable.Aggregate("[", (current, e) => current + (e + ","));
            str = str.Substring(0, str.Length - 1) + "]";
            Console.WriteLine(str);
#endif
        }

        public static IEnumerable<IEnumerable<T>> SplitCollection<T>(this IEnumerable<T> enumerable, IEnumerable<int> length)
        {
            var ts = enumerable as T[] ?? enumerable.ToArray();
            var ints = length as int[] ?? length.ToArray();
            if (ints.Sum() != ts.Length)
                throw new ArithmeticException();
            var pos = 0;
            foreach (var l in ints)
            {
                var tmp = new List<T>();
                for (var i = 0; i < l; i++)
                {
                    tmp.Add(ts[pos++]);
                }
                yield return tmp;
            }
        }

        public static T CastTo<T>(this object obj)
        {
            return (T) obj;
        }
        public static string GetMultiDimensionString(this IEnumerable enumerable)
        {
            var s = "[";
            var count = 0;
            foreach (var e in enumerable)
            {
                if (e.GetType().GetInterface("IEnumerable") == null)
                {
                    s += e + "";
                }
                else
                {
                    s += GetMultiDimensionString((IEnumerable)e);
                }

                count++;
                s += " ,";
            }

            if (count != 0)
                s = s.Remove(s.Length - 1);
            s += "]";
            return s;
        }
        public static void PrintMultiDimensionCollectionToConsole<T>(this IEnumerable<T> enumerable)
        {
            $"{enumerable.GetMultiDimensionString()}".PrintToConsole();
            
        }
        public static IEnumerable<TResult> ResultSaveAggregate<TSource, TResult>
            (this IEnumerable<TSource> enumerable, Func<TResult,TSource, TResult> func, TResult start)
        {
            var ans = new List<TResult>();
            var cur = start;
            foreach (var e in enumerable)
            {
                var tmp = func.Invoke(cur, e);
                ans.Add(tmp);
                cur = tmp;
            }

            return ans;
        }

        public static void SetWhere<TSource>(this IEnumerable<TSource> enumerable, Action<TSource> func, Func<TSource, bool> logicJudge)
        {
            foreach (var e in enumerable)
            {
                if(logicJudge(e))
                    func(e);
            }
        }

        public static int SearchFirstIndex<TSource>(this IEnumerable<TSource> enumerable, TSource element, int start = 0)
        {
            return enumerable.ToList().IndexOf(element, start);
        }

        public static (int index, TSource data) FindFirst<TSource>(this IEnumerable<TSource> enumerable,
            Func<TSource,int, bool> judge)
        {
            var t = enumerable.ToList();
            for (var i = 0; i < t.Count; i++)
            {
                if (judge(t[i], i))
                    return (i, t[i]);
            }
            return (-1, default);
        }
        
    }
}