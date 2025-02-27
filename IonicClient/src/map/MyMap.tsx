import { GoogleMap } from "@capacitor/google-maps";
import { useEffect, useRef } from "react";
import { mapsApiKey } from "../../mapsApiKey";
import { loadGoogleMaps } from "./loadGoogleMaps";


interface MyMapProps {
    lat: number,
    lng: number,
    onMapClick: (e: any) => void,
    onMarkerClick: (e: any) => void,
}

const MyMap: React.FC<MyMapProps> = ({ lat, lng, onMapClick, onMarkerClick }) => {
    const mapRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        loadGoogleMaps(mapsApiKey)
            .then(() => initializeMap())
            .catch((error) => console.error("Failed to load Google Maps:", error));
    }, []);

    const initializeMap = async () => {
        const googleMap = new google.maps.Map(mapRef.current!, {
            center: { lat, lng },
            zoom: 8,
        });

        googleMap.addListener("click", (event: google.maps.MapMouseEvent) => {
            if (event.latLng) {
                onMapClick({ latitude: event.latLng.lat(), longitude: event.latLng.lng() });
            }
        });

        const marker = new google.maps.Marker({
            position: { lat, lng },
            map: googleMap,
            title: "Selected Location",
        });

        marker.addListener("click", () => {
            onMarkerClick({ latitude: lat, longitude: lng });
        });
    };

    return <div ref={mapRef} style={{ width: "100%", height: "400px" }} />;
};

export default MyMap;