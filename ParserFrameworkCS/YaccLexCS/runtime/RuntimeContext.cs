using System.Collections.Generic;
using YaccLexCS.runtime.types;

namespace YaccLexCS.runtime
{
    public static class RuntimeStatusCode
    {
        public static readonly int DefinGlobalFunction = 0x1;
        public static readonly int Returning = 0x2;
    }
    public class RuntimeContext
    {
        public abstract class Frame{

        }
        public abstract class LinkedFrame
        {

        }
        public Dictionary<string, HashSet<FunctionThunk>> globalFunction = new Dictionary<string, HashSet<FunctionThunk>>();
        public readonly CommonStackFrame ModuleGlobalFrame = null;
        public int RuntimeStatus = 0x0;
        public void DefineGlobalFunction(FunctionThunk function)
        {
            if (!globalFunction.ContainsKey(function.FunctionName))
            {
                globalFunction.Add(function.FunctionName, new HashSet<FunctionThunk>());
            }
            globalFunction[function.FunctionName].Add(function);
        }

        public FunctionThunk? GetGlobalFunction(string name, int paramsCount)
        {
            if (!globalFunction.ContainsKey(name)) return null;
            var f = globalFunction[name];
            return f.FirstOrDefault(e => e.ParamsCount == paramsCount ,null);
        }

        public CommonStackFrame CreateNewStackFrame()
        {
            var frame = new CommonStackFrame(ModuleGlobalFrame);
            _stackFrames.Push(frame);
            return frame;
        }
        public CommonStackFrame PopStackFrame()
        {
            return _stackFrames.Pop();
        }
        public class CommonStackFrame : LinkedFrame
        {
            public Stack<Dictionary<string, object>> _linkedLocalStorage = new();
            public CommonStackFrame GlobalFrame = null;
            public CommonStackFrame()
            {
                CreateNewStorageBlockForNewCodeBlock();
            }
            public CommonStackFrame(CommonStackFrame gFrame)
            {
                CreateNewStorageBlockForNewCodeBlock();
                GlobalFrame = gFrame;
            }
            public Dictionary<string, object> CreateNewStorageBlockForNewCodeBlock()
            {
                var s = new Dictionary<string, object>();
                s["lexical_base_memory"] = new List<object>();
                _linkedLocalStorage.Push(s);
                return s;
            }
            public void RemoveNewestStorageBlock()
            {
                if (!_linkedLocalStorage.Any()) 
                    throw new Exception();
                _linkedLocalStorage.Pop();
            }

            public object? GetLocalVarLexical(int traceBackDepth, int order)
            {
                //Console.WriteLine($"Try get {(traceBackDepth, order)}, cur block stack depth = {_linkedLocalStorage.Count}");
                Dictionary<string, object> memory = null;
                if (traceBackDepth == _linkedLocalStorage.Count())
                {
                    //need to find in global area
                    memory = GlobalFrame._linkedLocalStorage.Last();
                }
                else
                {
                    memory = _linkedLocalStorage.Skip(traceBackDepth).First();
                }
                var lexicalMemory = memory["lexical_base_memory"] as List<object>;
                if(order < lexicalMemory!.Count())
                {
                    return lexicalMemory[order];
                }
                return null;
            }
            public void SetLocalVarLexical(int traceBackDepth, int order, object val)
            {
                //Console.WriteLine($"process dis={(traceBackDepth, order)} = {val},cur block stack depth = {_linkedLocalStorage.Count}");
                Dictionary<string, object> memory = null;
                if (traceBackDepth == _linkedLocalStorage.Count())
                {
                    //need to find in global area
                    memory = GlobalFrame._linkedLocalStorage.Last();
                }
                else
                {
                    memory = _linkedLocalStorage.Skip(traceBackDepth).First();
                }
                
                var lexivalMemory = memory["lexical_base_memory"] as List<object>;
                while (order >= lexivalMemory!.Count())
                    lexivalMemory!.Add(new());
                
                lexivalMemory[order] = val;
                
                return;
            }
            public void FindAndSetVarLexical(int depth, int order, object val)
            {
                SetLocalVarLexical(depth, order, val);
            }

            public object? GetLocalVar(string name)
            {
                var d = 0;
                foreach(var storage in _linkedLocalStorage)
                {
                    if (storage.ContainsKey(name))
                        return storage[name];
                    d++;
                }
                return null;
            }
            /*SetLocalVar will allocate a new var in currentBlock. It correspondent to var/let*/
            public dynamic SetLocalVar(string name, object val)
            {
                var localVarStorage = _linkedLocalStorage.Peek();
                if (localVarStorage.ContainsKey(name))
                {
                    return EEStatusValue.REDUPLICATE_VAR_DEF;
                }
                localVarStorage[name] = val;
                return val;
            }
          
          
            public void FindAndSetVar(string name, object val)
            {
                foreach (var storage in _linkedLocalStorage)
                {
                    var s = storage;
                    if (s.ContainsKey(name))
                    {
                        s[name] = val;
                        return;
                    }
                }
                _linkedLocalStorage.Peek()[name] = val;
            }
        }
/*
        private static Dictionary<string, object> ToValStorage(Dictionary<string, object> storage) =>
            (Dictionary<string, object>)storage["val"];
        private static Dictionary<string, HashSet<FunctionThunk>> ToFuncStorage(Dictionary<string,object> storage) =>
            (Dictionary<string, HashSet<FunctionThunk>>)storage["func"];*/

        public class RuntimeMemory
        {
            private readonly Dictionary<string, object> _kvComp = new();
           
            public object? this[string key]
            {
                get  { return _kvComp.ContainsKey(key) ? _kvComp[key] : null; }
 
                set => _kvComp[key] = value;
            }


           
        }
        private readonly RuntimeMemory _runtimeMemory = new();
        private Stack<CommonStackFrame> _stackFrames = new();

        public RuntimeContext()
        {
            this["v_tokenSourceText"] = "";
            this["v_tokenVal"] = null!;
            _stackFrames.Push(new ());
            ModuleGlobalFrame = _stackFrames.First();
            ModuleGlobalFrame.GlobalFrame = ModuleGlobalFrame;
        }

        public CommonStackFrame GetCurrentCommonFrame()
        {
            return _stackFrames.Peek();
        }
        public object? this[string key]
        {
            get => _runtimeMemory[key];
            set => _runtimeMemory[key] = value;
        }


        public Boolean RuntimeMemoryContains(string key)
        {
            return _runtimeMemory[key] != null;
        }
        public string TokenText
        {
            get => (string) this["v_tokenSourceText"];
            set => this["v_tokenSourceText"] = value;
        }
          

        public dynamic TokenVal
        {
            get => this["v_tokenVal"];
            set => this["v_tokenVal"] = value;
        }

        public void SetLocalVar(string name, object val)
        {

        }
    }
}