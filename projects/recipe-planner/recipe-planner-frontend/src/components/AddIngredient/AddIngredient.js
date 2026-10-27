import React from 'react';
import PropTypes from 'prop-types';
import './AddIngredient.css';
import {Button, Col, Form, Row} from "react-bootstrap";

const AddIngredient = ({ingredient, updateIngredient, removeIngredient}) => {

    const handleChange = (field) => (event) => {
        updateIngredient({...ingredient, [field]: event.target.value});
    };

    return (
        <Row>
            <Col>
                <Form.Group className="mb-1" controlId="formBasicName">
                    <Form.Control
                        placeholder="Name"
                        value={ingredient.ingredient}
                        onChange={handleChange('ingredient')}
                    />
                </Form.Group>
            </Col>
            <Col>
                <Form.Group className="mb-1" controlId="formBasicUnit">
                    <Form.Select value={ingredient.unit} onChange={handleChange('unit')}>
                        <option>PIECE</option>
                        <option>GRAMM</option>
                        <option>KILOGRAMM</option>
                        <option>LITRE</option>
                        <option>DECILITRE</option>
                    </Form.Select>
                </Form.Group>
            </Col>
            <Col>
                <Form.Group className="mb-1" controlId="quantity">
                    <Form.Control
                        type="number"
                        min="0"
                        placeholder="Quantity"
                        value={ingredient.quantity}
                        onChange={handleChange('quantity')}
                    />
                </Form.Group>
            </Col>
            <Col xs={1}>
                <Button
                    type="button"
                    onClick={() => removeIngredient(ingredient)}
                    variant='outline-dark'
                    className="mb-1"
                >x</Button>
            </Col>
        </Row>
    )
}

AddIngredient.propTypes = {
    ingredient: PropTypes.shape({
        listId: PropTypes.number,
        ingredient: PropTypes.string,
        unit: PropTypes.string,
        quantity: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    }).isRequired,
    updateIngredient: PropTypes.func.isRequired,
    removeIngredient: PropTypes.func.isRequired,
};

export default AddIngredient;
