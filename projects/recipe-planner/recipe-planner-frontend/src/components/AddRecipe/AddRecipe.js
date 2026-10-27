import React, {useEffect, useState} from 'react';
import './AddRecipe.css';
import {useForm} from "react-hook-form";
import {useNavigate, useParams} from "react-router-dom";
import {Form, Button, Col, Row} from 'react-bootstrap';

import axios from "axios";
import AddIngredient from "../AddIngredient/AddIngredient";

const API_URL = "http://localhost:8080/api/recipes";

function AddRecipe() {
    const navigate = useNavigate();
    const {id} = useParams();
    const isEdit = id !== undefined;

    const [ingredientRows, setIngredientRows] = useState([]);
    const [listId, setListId] = useState(1);
    const [submitError, setSubmitError] = useState(null);
    const [loadError, setLoadError] = useState(null);

    const {
        register,
        handleSubmit,
        reset,
        formState: {errors},
    } = useForm()

    useEffect(() => {
        if (!isEdit) {
            return;
        }
        axios.get(`${API_URL}/recipe/${id}`)
            .then((response) => {
                const recipe = response.data;
                if (!recipe) {
                    setLoadError('Dieses Rezept wurde nicht gefunden.');
                    return;
                }
                reset({
                    name: recipe.name,
                    description: recipe.description,
                    imageUrl: recipe.imageUrl
                });
                const rows = recipe.ingredients.map((ingredient, index) => ({
                    listId: index + 1,
                    id: ingredient.id,
                    comment: ingredient.comment,
                    ingredient: ingredient.name,
                    unit: ingredient.unit,
                    quantity: ingredient.amount
                }));
                setIngredientRows(rows);
                setListId(rows.length + 1);
            })
            .catch(() => setLoadError('Rezept konnte nicht geladen werden.'));
    }, [id, isEdit, reset]);

    const addIngredient = () => {
        setIngredientRows([
            ...ingredientRows,
            {listId: listId, ingredient: '', unit: 'PIECE', quantity: ''}
        ])
        setListId(listId + 1)
    }

    const updateIngredient = (ingredientObj) => {
        setIngredientRows(ingredientRows.map((row) =>
            row.listId === ingredientObj.listId ? ingredientObj : row
        ))
    }

    const removeIngredient = (ingredientObj) => {
        setIngredientRows(ingredientRows.filter((row) => row.listId !== ingredientObj.listId))
    }

    const onSubmit = (data) => {
        setSubmitError(null)

        const recipe = {
            name: data.name,
            description: data.description,
            imageUrl: data.imageUrl,
            ingredients: ingredientRows.map(({id: ingredientId, comment, ingredient, unit, quantity}) => ({
                id: ingredientId,
                name: ingredient,
                comment: comment ?? '',
                unit: unit,
                amount: Number(quantity) || 0
            }))
        }

        const request = isEdit
            ? axios.put(`${API_URL}/${id}`, recipe)
            : axios.post(API_URL, recipe)

        request
            .then(() => navigate('/'))
            .catch(() => setSubmitError('Rezept konnte nicht gespeichert werden. Bitte nochmals versuchen.'))
    }

    const renderIngredients = ingredientRows.map(row => <AddIngredient
        key={row.listId}
        ingredient={row}
        updateIngredient={updateIngredient}
        removeIngredient={removeIngredient}
    />)

    return (
        <>
            <div className="bg">
                <div className="m-3">
                    <h1 className="h3 bg-dark text-bg-primary mt-2">{isEdit ? 'Edit Recipe' : 'Add Recipe'}</h1>
                    {loadError && <div className="text-danger mt-3">{loadError}</div>}
                    <Form onSubmit={handleSubmit(onSubmit)}>
                        <Form.Group className="mb-1" controlId="formBasicName">
                            <Form.Label>Recipe Name:</Form.Label>
                            <Form.Control
                                placeholder="Name"
                                isInvalid={!!errors.name}
                                {...register('name', {required: true})}
                            />
                            <Form.Control.Feedback type="invalid">
                                Bitte einen Namen angeben.
                            </Form.Control.Feedback>
                        </Form.Group>
                        <Form.Group className="mb-1" controlId="formBasicDescription">
                            <Form.Label>Description:</Form.Label>
                            <Form.Control
                                placeholder="Description"
                                isInvalid={!!errors.description}
                                {...register('description', {required: true})}
                            />
                            <Form.Control.Feedback type="invalid">
                                Bitte eine Beschreibung angeben.
                            </Form.Control.Feedback>
                        </Form.Group>
                        <Form.Group className="mb-1 mb-5" controlId="formBasicImageUrl">
                            <Form.Label>Image URL:</Form.Label>
                            <Form.Control placeholder="URL" {...register('imageUrl')} />
                        </Form.Group>
                        <Row>
                            <Col>Ingredient</Col>
                            <Col>Unit</Col>
                            <Col>Quanity</Col>
                            <Col xs={1}></Col>
                        </Row>
                        <hr/>
                        <Row>
                            <br></br>
                        </Row>
                        {renderIngredients}
                        <Row>
                            <br></br>
                            <Button
                                type="button"
                                variant='warning'
                                onClick={addIngredient}
                                className="mt-1"
                            >Add Ingredient</Button>
                        </Row>
                        {submitError && <div className="text-danger mt-3">{submitError}</div>}
                        <Button variant="primary" type="submit" className="mb-5 mt-3">
                            {isEdit ? 'Save' : 'Submit'}
                        </Button>
                    </Form>
                </div>

            </div>

        </>
    )
}

export default AddRecipe;
