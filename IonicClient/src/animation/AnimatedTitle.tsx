import { CreateAnimation } from "@ionic/react"
import { useEffect, useRef } from "react"

export const AnimatedTitle: React.FC = () => {
    const animationRef = useRef<CreateAnimation>(null);

    useEffect(() => {
        if (animationRef.current) {
            animationRef.current.animation.play();
        }
    }, []);

    return (
        <CreateAnimation
            ref={animationRef}
            duration={1000}
            iterations={Infinity}
            keyframes={[
                { offset: 0, opacity: '0', transform: 'scale(0.5)' },
                { offset: 0.5, opacity: '0.5' , transform: 'scale(1.2)' },
                { offset: 1, opacity: '1', transform: 'scale(1)' },
            ]}
        >
            <h1 style={{'padding': '0 20px'}}>Task Manager</h1>
        </CreateAnimation>
    );
};