# Define variables
VINEFLOWER_JAR = vineflower-1.11.2.jar
JARS_DIR = ../magnolia-webapp/target/magnolia-webapp-1.0.0-SNAPSHOT/WEB-INF/lib
DECOMPILED_DIR = ../decompiled

# Default target
all: decompile

# Decompile each Magnolia JAR to a subfolder named after the JAR (without .jar extension)
decompile:
	@if [ ! -d "$(JARS_DIR)" ]; then \
		echo "Error: Cannot find JARs directory. Please build magnolia first"; \
		exit 1; \
	fi
	@if ! ls $(JARS_DIR)/magnolia*.jar 1> /dev/null 2>&1; then \
		echo "Error: No Magnolia JARs found in $(JARS_DIR). Please build magnolia first"; \
		exit 1; \
	fi
	@echo "Decompiling Magnolia JARs to individual folders..."
	@mkdir -p $(DECOMPILED_DIR)
	for jar in $(JARS_DIR)/magnolia*.jar; do \
		if [ -f "$$jar" ]; then \
			base=$$(basename "$$jar" .jar); \
			echo "Decompiling $$base..."; \
			mkdir -p $(DECOMPILED_DIR)/$$base; \
			java -jar $(VINEFLOWER_JAR) --folder $$jar $(DECOMPILED_DIR)/$$base; \
		fi \
	done
	@echo "Decompilation completed."

# Clean target to remove decompiled files
clean:
	@echo "Cleaning up..."
	@rm -rf $(DECOMPILED_DIR)
	@echo "Cleanup completed."