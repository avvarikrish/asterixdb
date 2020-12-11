package org.apache.asterix.api.http.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.apache.hyracks.http.api.IServletRequest;
import org.apache.hyracks.http.api.IServletResponse;
import org.apache.hyracks.http.server.AbstractServlet;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

public class MetricsServlet extends AbstractServlet {
    private CollectorRegistry registry;

    public MetricsServlet(ConcurrentMap<String, Object> ctx, String... paths) {
        this(CollectorRegistry.defaultRegistry, ctx, paths);
    }

    public MetricsServlet(CollectorRegistry registry, ConcurrentMap<String, Object> ctx, String... paths) {
        super(ctx, paths);
        this.registry = registry;
    }

    @Override
    protected void get(IServletRequest request, IServletResponse response) throws IOException {
        response.setHeader(HttpHeaderNames.CONTENT_TYPE, TextFormat.CONTENT_TYPE_004);

        Writer writer = new BufferedWriter(response.writer());
        try {
            TextFormat.write004(writer, registry.filteredMetricFamilySamples(parse(request)));
            response.setStatus(HttpResponseStatus.OK);
            writer.flush();
        } finally {
            writer.close();
        }
    }

    private Set<String> parse(IServletRequest req) {
        List<String> includedParam = req.getParameterValues("name[]");
        if (includedParam == null) {
            return Collections.emptySet();
        } else {
            return new HashSet<String>(includedParam);
        }
    }

    @Override
    protected void post(IServletRequest req, IServletResponse resp) throws IOException {
        get(req, resp);
    }
}
