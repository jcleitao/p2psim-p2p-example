# p2psim-p2p-example

This project provides examples of simple peer-to-peer protocols (Cyclon and an epidemic flood protocol operating on top of it) and configuration files to execute simple benchmark experiments on the Peernet simulator (https://github.com/jcleitao/p2psim-core) using its (original) event-driven simulation and the scalable event-driven engine available here: https://github.com/jcleitao/p2psim-scalable-engine.

The experiments defined in the configuration files allow to execute experiments that stress the simualtor, by having nodes form an unstructured overlay network based on Cyclon (with 30 neighbors) and then executing a costly epidemic flood broadcast protocol on top of that overlay, where 10% of nodes broadcast a message periodically.

This project is being conducted by the NOVA-SYS research group (https://novasys.di.fct.unl.pt/) of the NOVA LINCS laboratory (https://nova-lincs.di.fct.unl.pt/) from the NOVA School of Science and Technology (https://www.fct.unl.pt/en/about-fct/overview) in Portugal.

Contact person: João Leitão (https://asc.di.fct.unl.pt).

This iniative is being pursued in colaboration with Protocol Labs (https://protocol.ai/), in particular with the ResNet Lab (https://research.protocol.ai/groups/resnetlab/).
