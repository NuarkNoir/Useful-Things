using System.Collections.Generic;

namespace Tests
{
    class AndroidApp
    {
        string name, version;
        List<string> tags;

        public AndroidApp(string name, List<string> tags, string version)
        {
            this.name = name;
            this.tags = tags;
            this.version = version;
        }

        public string getName() { return name; }

        public string getVersion() { return version; }

        public List<string> gettags() { return tags; }
    }
}
