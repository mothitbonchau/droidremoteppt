using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using log4net.Config;
using log4net.Core;
using log4net.Layout;

namespace droidRemotePPT.Server
{
    public static class Logging
    {
        private const string LOG_PATTERN = "%-5p %m%n";
        public static log4net.ILog Root { get; private set; }
        public static log4net.Appender.MemoryAppender MemAppender { get; private set; }

        static Logging()
        {
            Configure();

            Root = log4net.LogManager.GetLogger("Root");
        }

        public static void Configure()
        {
            var hierarchy = (log4net.Repository.Hierarchy.Hierarchy)log4net.LogManager.GetRepository();
            var patternLayout = new PatternLayout();
            patternLayout.ConversionPattern = LOG_PATTERN;
            patternLayout.ActivateOptions();

            MemAppender = new log4net.Appender.MemoryAppender();
            MemAppender.Name = "MemoryAppender";
            MemAppender.Layout = patternLayout;
            MemAppender.ActivateOptions();
            hierarchy.Root.AddAppender(MemAppender);

            var traceAppender = new log4net.Appender.TraceAppender();
            traceAppender.Name = "TraceAppender";
            traceAppender.Layout = patternLayout;
            traceAppender.ActivateOptions();
            hierarchy.Root.AddAppender(traceAppender);

            hierarchy.Root.Level = Level.All;
            hierarchy.Configured = true;
        }
    }
}
