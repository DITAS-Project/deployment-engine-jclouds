package com.atos.deployment.frontend.rest;

import com.google.inject.Inject;

import com.atos.deployment.infrastructure.beans.Deployment;
import com.atos.deployment.infrastructure.beans.DeploymentInfo;
import com.atos.deployment.infrastructure.beans.Infrastructure;
import com.atos.deployment.infrastructure.deployers.CloudSigmaDeployer;
import com.atos.deployment.infrastructure.deployers.Deployer;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.Success;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

@Controller("/v2")
public class DeploymentRestService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentRestService.class);

    @Inject
    MongoClient dbClient;

    @Post(value = "/deployment", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<DeploymentInfo> deploy(@Body Deployment deployment) {

        MongoCollection<DeploymentInfo> collection = dbClient.getDatabase("deployment-engine").getCollection("deployment", DeploymentInfo.class);

        DeploymentInfo result = new DeploymentInfo();
        result.setInfrastructures(new ArrayList<>());
        collection.insertOne(result).subscribe(new Subscriber<Success>() {
            @Override
            public void onSubscribe(Subscription s) {

            }

            @Override
            public void onNext(Success success) {

            }

            @Override
            public void onError(Throwable t) {
                logger.error("Error saving initial deployment", t);
            }

            @Override
            public void onComplete() {
                for (Infrastructure infra : deployment.getInfrastructure()) {
                    Deployer deployer = null;
                    switch (infra.getApiType()) {
                        case CLOUDSIGMA:
                            try {
                                deployer = new CloudSigmaDeployer();
                            } catch (ConfigurationException e) {
                                logger.error("Error configuring cloudsigma deployer", e);
                            }
                            break;
                    }
                    if (deployer != null) {
                        result.getInfrastructures().add(deployer.deploy(infra));
                    }
                }
            }
        });


        return HttpResponse.ok(result);
    }
}