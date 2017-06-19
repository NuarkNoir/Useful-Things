using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Tests
{
    class TrashboxDump
    {
        public string name = "Trashbox";
        public double version = 10.2;
        public List<AndroidApp> programms;
        public List<AndroidApp> games;

        public TrashboxDump(List<AndroidApp> programms/*, List<AndroidApp> games*/)
        {
            this.programms = programms;
            //this.games = games;
        }

        public string getName() { return name; }

        public double getVersion() { return version; }

        public List<AndroidApp> getProgramms() { return programms; }

        public List<AndroidApp> getGames() { return games; }
    }
}
