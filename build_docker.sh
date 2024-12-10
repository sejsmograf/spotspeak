DOCKERFILE="Dockerfile.one-stage"
IMAGE_NAME="spotspeak"

# Parse command-line arguments
while [[ "$#" -gt 0 ]]; do
  case $1 in
    --full-build)
      DOCKERFILE="Dockerfile.two-stage"
      ;;
    --help)
      echo "Usage: $0 [--full-build]"
      echo "       --full-build  Perform a full two-stage build using Dockerfile.two"
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      echo "Use --help for usage information."
      exit 1
      ;;
  esac
  shift
done

# Build the Docker image
echo "Using Dockerfile: $DOCKERFILE"

if [ "$DOCKERFILE" == "Dockerfile.two-stage" ]; then
  docker build --no-cache -t $IMAGE_NAME -f $DOCKERFILE .
else
  docker build --no-cache -t $IMAGE_NAME -f $DOCKERFILE .
fi
