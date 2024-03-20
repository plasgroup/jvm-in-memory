using System;
using System.Collections.Generic;

namespace YaccLexCS.ycomplier.automata
{
    [Serializable]
    public class AutomataContext
    {
        private readonly Dictionary<object, object> _kvMemory = new();

        public bool ContainsKey(object key)
        {
            return _kvMemory.ContainsKey(key);
        }
        public object this[object obj]
        {
            get => _kvMemory[obj];
            set => _kvMemory[obj] = value;
        }
        public void ResetContext()
        {
            _kvMemory.Clear();
        }
    }
}