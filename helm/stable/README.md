This Helm chart is used to deploy Magnolia on all envs.

Pod (cpu, memory) and java heap size can be customized per env using individaul values files e.g `values_DEV.yaml` 

Common parameters for all environments are always used and defined in `values.yaml` \
Individual `values_ENV.yaml` overwrites configured parameters.
