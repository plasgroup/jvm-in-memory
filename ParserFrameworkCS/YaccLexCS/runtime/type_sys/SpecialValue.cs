namespace YaccLexCS.runtime
{
    public enum SpecialValue
	{
		BREAK,
		CONTINUE,
		NoMatch
	}
	public struct ReturnVal
    {
		public object Value;

        public ReturnVal(object value)
        {
            Value = value;
        }
    }
	public enum EEStatusValue
	{
		REDUPLICATE_VAR_DEF
	}
}